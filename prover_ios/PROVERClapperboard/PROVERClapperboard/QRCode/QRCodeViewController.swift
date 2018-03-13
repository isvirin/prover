import UIKit

class QRCodeViewController: UIViewController {
  
  // MARK: - IBOutlet
  @IBOutlet weak var qrText: UILabel! {
    didSet {
      qrText.text = text
    }
  }
  
  // MARK: - IBAction
  @IBAction func closeButtonAction(_ sender: UIButton) {
    dismiss(animated: true, completion: nil)
  }
  
  // MARK: - Instance properties
  var text: String?
  
  // MARK: - Lifecycle
  override func viewDidLoad() {
    super.viewDidLoad()
  }
}
