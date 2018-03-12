import UIKit

class QRCodeViewController: UIViewController {

  // MARK: - IBAction
  @IBAction func closeButtonAction(_ sender: UIButton) {
    print("close button touched")
    dismiss(animated: true, completion: nil)
  }
  
  // MARK: - Lifecycle
  override func viewDidLoad() {
    super.viewDidLoad()
    print("view did load")
  }
}
