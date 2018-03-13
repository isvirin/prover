import UIKit

class InputTextViewController: UIViewController {
  
  enum Segue: String {
    case createQRSegue
  }

  // MARK: - IBOutlet
  @IBOutlet weak var qrTextField: UITextField!

  // MARK: - IBAction
  @IBAction func endInputText(_ sender: UITextField) {
  }
  
  @IBAction func qrButtonAction(_ sender: Any) {
    performSegue(withIdentifier: Segue.createQRSegue.rawValue, sender: nil)
  }
  
  // MARK: - Lifecycle
  override func viewDidLoad() {
    super.viewDidLoad()
  }
  
  // MARK: - Segue
  override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
    
    guard let identifier = segue.identifier else { return }
    
    switch identifier {
    case Segue.createQRSegue.rawValue:
      if let destination = segue.destination as? QRCodeViewController {
        destination.text = qrTextField.text
      }
    default:
      fatalError("Unexpected segue")
    }
  }
}
