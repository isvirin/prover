import UIKit

class ExportWalletViewController: UITableViewController {
  
  // MARK: - IBOutlet
  @IBOutlet weak var walletAddress: UILabel! {
    didSet {
      walletAddress.text = store.ethereumService.hexAddress
    }
  }
  
  // MARK: - IBAction
  @IBAction func backButtonAction(_ sender: UIBarButtonItem) {
    navigationController?.popViewController(animated: true)
  }
  
  // MARK: - Dependency
  var store: DependencyStore!
}
