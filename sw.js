const CACHE_NAME = 'fruit-game-v4';
const DB_NAME = 'fruit-game-db';
const DB_VERSION = 2;
const STORE_NAME = 'resources';
const VERSION_STORE = 'version';

// キャッシュするリソース（すべてのリソースをインストール時にダウンロード）
const RESOURCES_TO_CACHE = [
    './',
    './index.html',
    './manifest.json',
    './images/apple.png',
    './images/banana.png',
    './version.json'
];

// IndexedDBを開く
function openDB() {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open(DB_NAME, DB_VERSION);

        request.onerror = () => reject(request.error);
        request.onsuccess = () => resolve(request.result);

        request.onupgradeneeded = (event) => {
            const db = event.target.result;
            if (!db.objectStoreNames.contains(STORE_NAME)) {
                db.createObjectStore(STORE_NAME, { keyPath: 'url' });
            }
            if (!db.objectStoreNames.contains(VERSION_STORE)) {
                db.createObjectStore(VERSION_STORE, { keyPath: 'key' });
            }
        };
    });
}

// IndexedDBにリソースを保存
async function saveToIndexedDB(url, response) {
    try {
        const db = await openDB();
        const blob = await response.clone().blob();
        const tx = db.transaction(STORE_NAME, 'readwrite');
        const store = tx.objectStore(STORE_NAME);

        await store.put({
            url: url,
            blob: blob,
            headers: Object.fromEntries(response.headers.entries()),
            status: response.status,
            statusText: response.statusText
        });

        return new Promise((resolve, reject) => {
            tx.oncomplete = () => resolve();
            tx.onerror = () => reject(tx.error);
        });
    } catch (error) {
        console.error('Error saving to IndexedDB:', error);
    }
}

// IndexedDBからリソースを取得
async function getFromIndexedDB(url) {
    try {
        const db = await openDB();
        const tx = db.transaction(STORE_NAME, 'readonly');
        const store = tx.objectStore(STORE_NAME);

        return new Promise((resolve, reject) => {
            const request = store.get(url);
            request.onsuccess = () => {
                const data = request.result;
                if (data) {
                    const response = new Response(data.blob, {
                        status: data.status,
                        statusText: data.statusText,
                        headers: data.headers
                    });
                    resolve(response);
                } else {
                    resolve(null);
                }
            };
            request.onerror = () => reject(request.error);
        });
    } catch (error) {
        console.error('Error getting from IndexedDB:', error);
        return null;
    }
}

// Service Workerのインストール
self.addEventListener('install', (event) => {
    console.log('Service Worker: Installing...');

    event.waitUntil(
        (async () => {
            // Cache APIにキャッシュ
            const cache = await caches.open(CACHE_NAME);
            console.log('Service Worker: Caching resources');
            await cache.addAll(RESOURCES_TO_CACHE);

            // IndexedDBにもキャッシュ
            for (const url of RESOURCES_TO_CACHE) {
                try {
                    const response = await fetch(url);
                    if (response.ok) {
                        await saveToIndexedDB(url, response);
                    }
                } catch (error) {
                    console.error(`Failed to cache ${url}:`, error);
                }
            }

            // インストール後すぐにアクティベート
            self.skipWaiting();
        })()
    );
});

// Service Workerのアクティベーション
self.addEventListener('activate', (event) => {
    console.log('Service Worker: Activating...');

    event.waitUntil(
        (async () => {
            // 古いキャッシュを削除
            const cacheNames = await caches.keys();
            await Promise.all(
                cacheNames
                    .filter(name => name !== CACHE_NAME)
                    .map(name => caches.delete(name))
            );

            // すべてのクライアントを制御下に置く
            await self.clients.claim();
        })()
    );
});

// フェッチイベントの処理
self.addEventListener('fetch', (event) => {
    event.respondWith(
        (async () => {
            try {
                // まずCache APIから試す
                const cachedResponse = await caches.match(event.request);
                if (cachedResponse) {
                    console.log('Service Worker: Serving from Cache API:', event.request.url);
                    return cachedResponse;
                }

                // 次にIndexedDBから試す
                const indexedDBResponse = await getFromIndexedDB(event.request.url);
                if (indexedDBResponse) {
                    console.log('Service Worker: Serving from IndexedDB:', event.request.url);
                    return indexedDBResponse;
                }

                // オンラインの場合はネットワークから取得
                console.log('Service Worker: Fetching from network:', event.request.url);
                const networkResponse = await fetch(event.request);

                // 成功したレスポンスをキャッシュに保存
                if (networkResponse.ok) {
                    const cache = await caches.open(CACHE_NAME);
                    cache.put(event.request, networkResponse.clone());
                    await saveToIndexedDB(event.request.url, networkResponse.clone());
                }

                return networkResponse;
            } catch (error) {
                console.error('Service Worker: Fetch failed:', error);

                // オフラインでキャッシュがない場合の処理
                return new Response('Offline - Resource not available', {
                    status: 503,
                    statusText: 'Service Unavailable',
                    headers: new Headers({
                        'Content-Type': 'text/plain'
                    })
                });
            }
        })()
    );
});
