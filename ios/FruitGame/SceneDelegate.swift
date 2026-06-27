import UIKit

/// 単一の全画面ウィンドウに `WebViewController` を載せるだけの Scene。
class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }
        let window = UIWindow(windowScene: windowScene)
        window.rootViewController = WebViewController()
        self.window = window
        window.makeKeyAndVisible()
    }
}
