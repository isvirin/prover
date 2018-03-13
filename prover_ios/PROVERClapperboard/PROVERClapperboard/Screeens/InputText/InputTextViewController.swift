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
  var store: Store!
  
  // MARK: - Lifecycle
  override func viewDidLoad() {
    super.viewDidLoad()
  }
  
  override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    navigationController?.setNavigationBarHidden(true, animated: false)
  }
  
  override func viewWillDisappear(_ animated: Bool) {
    super.viewDidDisappear(animated)
    navigationController?.setNavigationBarHidden(false, animated: false)
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
      print("Wallet segue")
    default:
      fatalError("Unexpected segue")
    }
  }
}
