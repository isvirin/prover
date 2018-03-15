import UIKit
import FileBrowser

class ImportWalletViewController: UITableViewController {
  
  // MARK: - IBOutlet
  @IBOutlet weak var walletFileLabel: UILabel!
  @IBOutlet weak var showHidePasswordButton: UIButton!
  @IBOutlet weak var passwordTextField: UITextField!
  
  // MARK: - IBAction
  @IBAction func backButtonAction(_ sender: UIBarButtonItem) {
    navigationController?.popViewController(animated: true)
  }
  
  @IBAction func endInputText(_ sender: UITextField) {
  }
  
  @IBAction func browserButtonAction(_ sender: UIButton) {
    print("Click on browse button")
    let fileBrowser = FileBrowser()
    fileBrowser.didSelectFile = { (file: FBFile) in
      self.walletFileLabel.text = file.displayName
      self.walletPath = file.filePath
    }
    present(fileBrowser, animated: true, completion: nil)
  }

  @IBAction func showHidePasswordButtonAction(_ sender: UIButton) {
    isPasswordSecured = !isPasswordSecured
  }
  
  // MARK: - Private properties
  var walletPath: URL? {
    didSet {
      print(walletPath)
    }
  }
  var isPasswordSecured = true {
    didSet {
      passwordTextField.isSecureTextEntry = isPasswordSecured
      switch isPasswordSecured {
      case true:
        showHidePasswordButton.setImage(#imageLiteral(resourceName: "show"), for: .normal)
      case false:
        showHidePasswordButton.setImage(#imageLiteral(resourceName: "hide"), for: .normal)
      }
    }
  }
  
  // MARK: - Lifecycle
  override func viewDidLoad() {
    super.viewDidLoad()
  }

}

// MARK: - UIDocumentPickerDelegate
extension ImportWalletViewController: UIDocumentPickerDelegate {
  
  func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentAt url: URL) {
    
    print("import result : \(url)")
  }
  
  func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
    
    print("view was cancelled")
    dismiss(animated: true, completion: nil)
  }
}
