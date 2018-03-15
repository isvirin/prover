import UIKit

class ExportWalletViewController: UITableViewController {
  
  // MARK: - IBOutlet
  @IBOutlet weak var walletAddress: UILabel! {
    didSet {
      walletAddress.text = store.ethereumService.hexAddress
    }
  }
  @IBOutlet weak var showHidePasswordButton: UIButton!
  @IBOutlet weak var passwordTextField: UITextField!
  
  // MARK: - IBAction
  @IBAction func backButtonAction(_ sender: UIBarButtonItem) {
    navigationController?.popViewController(animated: true)
  }
  
  @IBAction func endInputText(_ sender: UITextField) {
  }
  
  @IBAction func showHidePasswordButtonAction(_ sender: UIButton) {
    isPasswordSecured = !isPasswordSecured
  }
  
  @IBAction func cancelButtonAction(_ sender: UIButton) {
    navigationController?.popViewController(animated: true)
  }
  
  // MARK: - Private properties
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
  
  // MARK: - Dependency
  var store: DependencyStore!
}
