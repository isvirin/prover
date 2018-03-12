import UIKit

class InputTextViewController: UIViewController {

  // MARK: - IBOutlet
  @IBOutlet weak var qrTextField: UITextField!

  // MARK: - IBAction
  @IBAction func endInputTesx(_ sender: UITextField) {
  }
  
  // MARK: - Lifecycle
  override func viewDidLoad() {
    super.viewDidLoad()
  }
  
  override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    
    navigationController?.setNavigationBarHidden(true, animated: false)
  }
  
  override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    
    navigationController?.setNavigationBarHidden(false, animated: false)
  }
}
