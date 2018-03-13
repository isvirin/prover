import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

  var window: UIWindow?
  let store = Store()

  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
    
    if let navVC  = window?.rootViewController as? UINavigationController? {
      if let vc = navVC?.viewControllers.first as? InputTextViewController {
        vc.store = store
      }
    }
    
    return true
  }
}
