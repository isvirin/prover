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
  @IBOutlet weak var shareButton: UIButton! {
    didSet {
      let color = UIColor(red: 155 / 255, green: 155 / 255,
                          blue: 155 / 255, alpha: 1)
      shareButton.isEnabled = false
      shareButton.setTitleColor(color, for: .disabled)
    }
  }
  
  // MARK: - IBAction
  @IBAction func backButtonAction(_ sender: UIBarButtonItem) {
    navigationController?.popViewController(animated: true)
  }
  
  @IBAction func endInputText(_ sender: UITextField) {
  }
  
  @IBAction func showHidePasswordButtonAction(_ sender: UIButton) {
    isPasswordSecured = !isPasswordSecured
  }
  
  @IBAction func saveButtonAction(_ sender: UIButton) {
    
    guard let password = passwordTextField.text, password != "" else {
      showAlert(with: "Please type password")
      return
    }
    
    guard let walletData = store.ethereumService.exportWallet(password: password) else {
      showAlert(with: "Can't export new wallet") { [weak self] (_) in
        self?.passwordTextField.text = ""
      }
      return
    }
    currentWallet = walletData
    
    self.passwordTextField.text = ""
    shareButton.isEnabled = true
  }
  
  @IBAction func cancelButtonAction(_ sender: UIButton) {
    navigationController?.popViewController(animated: true)
  }
  
  @IBAction func shareButtonAction(_ sender: UIButton) {

    guard let walletData = currentWallet else {
      print("There is no current wallet")
      return
    }
    let walletAddress = store.ethereumService.hexAddress
    
    let objectsToShare = [walletData, walletAddress] as [Any]
    let activityVC = UIActivityViewController(activityItems: objectsToShare, applicationActivities: nil)
    //    activityVC.excludedActivityTypes = [.airDrop, .addToReadingList]
    activityVC.popoverPresentationController?.sourceView = sender
    self.present(activityVC, animated: true, completion: nil)
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
  
  var currentWallet: Data?
  
  // MARK: - Dependency
  var store: DependencyStore!
}

// MARK: - Private methods
private extension ExportWalletViewController {
  
  func showAlert(with text: String,
                 title: String = "Error",
                 handler: ((UIAlertAction) -> Void)? = nil) {
    let alert = UIAlertController(title: title, message: text, preferredStyle: .alert)
    alert.addAction(UIAlertAction(title: "OK", style: .cancel, handler: handler))
    self.present(alert, animated: true, completion: nil)
  }
}
