import UIKit

class QRCodeViewController: UIViewController {

  // MARK: - IBAction
  @IBAction func closeButtonAction(_ sender: UIButton) {
    dismiss(animated: true, completion: nil)
  }
  
  // MARK: - Lifecycle
  override func viewDidLoad() {
    super.viewDidLoad()
  }
}
