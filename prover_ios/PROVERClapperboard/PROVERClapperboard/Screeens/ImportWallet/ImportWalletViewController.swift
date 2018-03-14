import UIKit

class ImportWalletViewController: UITableViewController {
  
  // MARK: - IBAction
  @IBAction func cancelButtonAction(_ sender: UIBarButtonItem) {
    navigationController?.popViewController(animated: true)
  }
  
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
