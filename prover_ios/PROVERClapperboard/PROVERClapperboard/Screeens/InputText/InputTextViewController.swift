import UIKit

class InputTextViewController: UIViewController {

  // MARK: - IBOutlet
  @IBOutlet weak var qrTextField: UITextField!

  // MARK: - IBAction
  @IBAction func walletButtonAction(_ sender: UIButton) {
    performSegue(withIdentifier: Segue.walletSegue.rawValue, sender: nil)
  }
  
  @IBAction func endInputText(_ sender: UITextField) {
  }
  
  @IBAction func qrButtonAction(_ sender: Any) {
    performSegue(withIdentifier: Segue.createQRSegue.rawValue, sender: nil)
  }
  
  // MARK: - Dependency
  var store: DependencyStore!
  
  // MARK: - Lifecycle
  override func viewDidLoad() {
    super.viewDidLoad()
  }
  
  // MARK: - Segue
  enum Segue: String {
    case createQRSegue
    case walletSegue
  }
  
  override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
    
    guard let identifier = segue.identifier else { return }
    
    switch identifier {
    case Segue.createQRSegue.rawValue:
      if let destination = segue.destination as? QRCodeViewController {
        destination.text = qrTextField.text
        destination.store = store
      }
    case Segue.walletSegue.rawValue:
      if let navigatinVC = segue.destination as? UINavigationController,
        let destination = navigatinVC.viewControllers.first as? WalletViewController {
        destination.store = store
      }
    default:
      fatalError("Unexpected segue")
    }
  }
}
