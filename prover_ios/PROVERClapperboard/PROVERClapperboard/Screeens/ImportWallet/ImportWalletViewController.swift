import UIKit
import FileBrowser

class ImportWalletViewController: UITableViewController {
  
  // MARK: - IBOutlet
  @IBOutlet weak var walletFileLabel: UILabel!
  
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
//      self.dismiss(animated: true, completion: nil)
    }
    present(fileBrowser, animated: true, completion: nil)
  }
  
  // MARK: - Private properties
  var walletPath: URL? {
    didSet {
      print(walletPath)
    }
  }
  
  // MARK: - Lifecycle
  override func viewDidLoad() {
    super.viewDidLoad()
  }

}

// MARK: - UITableViewDelegate
extension ImportWalletViewController {
  
  override func tableView(_ tableView: UITableView,
                          heightForRowAt indexPath: IndexPath) -> CGFloat {
    return UITableViewAutomaticDimension
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
