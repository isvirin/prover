import UIKit
import SafariServices

class WalletViewController: UITableViewController {
  
  // MARK: - IBOutlet
  private let balanceTitleLabel: UILabel = {
    let label = UILabel()
    label.text = "Balance"
    label.textColor = .white
    label.font = UIFont.systemFont(ofSize: 11)
    label.translatesAutoresizingMaskIntoConstraints = false
    return label
  }()
  
  private let balanceLabel: UILabel = {
    let label = UILabel()
    label.text = "0.0 P"
    label.textColor = .white
    label.font = UIFont.systemFont(ofSize: 30)
    label.translatesAutoresizingMaskIntoConstraints = false
    return label
  }()
  
  @IBOutlet weak var walletAddress: UILabel! {
    didSet {
      walletAddress.text = store.ethereumService.hexAddress
    }
  }
  
  // MARK: - IBAction
  @IBAction func backButtonAction(_ sender: UIBarButtonItem) {
    navigationController?.dismiss(animated: true, completion: nil)
  }
  
  @IBAction func copyButtonAction(_ sender: UIButton) {
    UIPasteboard.general.string = walletAddress.text
  }
  
  // MARK: - Dependency
  var store: DependencyStore!
  
  // MARK: - Lifecycle
  override func viewDidLoad() {
    super.viewDidLoad()
    
    configureNavigationBar()
    configureNavigationTitle()
    
    tableView.backgroundColor = view.backgroundColor
  }
  
  private func configureNavigationBar() {
    guard let navigationBar = navigationController?.navigationBar else { return }
    let size = CGSize(width: navigationBar.bounds.width,
                      height: navigationBar.bounds.height + UIApplication.shared.statusBarFrame.height)
    let image = #imageLiteral(resourceName: "background").resizedImage(newSize: size)
    navigationBar.barTintColor = UIColor(patternImage: image)
    navigationBar.tintColor = .white
  }
  
  // MARK: - Segue
  enum Segue: String {
    case importWalletSegue
  }
  
  // MARK: - Private methods
  private func configureNavigationTitle() {
    
    guard let bar = navigationController?.navigationBar else { return }
    
    bar.addSubview(balanceTitleLabel)
    bar.addSubview(balanceLabel)
    
    balanceTitleLabel.topAnchor
      .constraint(equalTo: bar.topAnchor, constant: 10).isActive = true
    balanceTitleLabel.leftAnchor
      .constraint(equalTo: bar.leftAnchor, constant: 100).isActive = true
    balanceTitleLabel.rightAnchor
      .constraint(equalTo: bar.rightAnchor, constant: 100).isActive = true
    
    balanceLabel.topAnchor
      .constraint(equalTo: balanceTitleLabel.bottomAnchor, constant: 2).isActive = true
    balanceLabel.leftAnchor
      .constraint(equalTo: balanceTitleLabel.leftAnchor).isActive = true
    balanceLabel.rightAnchor
      .constraint(equalTo: balanceTitleLabel.rightAnchor).isActive = true
  }
  
  private func showSafariView() {
    print("show safari view")
    guard let url = URL(string: "https://mvp.prover.io/#get_ropsten_testnet_ether") else {
      print("Can't create URL")
      return
    }
    let safariView = SFSafariViewController(url: url)
    present(safariView, animated: true, completion: nil)
  }
}

// MARK: - UITableViewDelegate
extension WalletViewController {
  
  override func tableView(_ tableView: UITableView,
                          heightForRowAt indexPath: IndexPath) -> CGFloat {
    if indexPath.row == 0 {
      return UITableViewAutomaticDimension
    } else {
      return 48
    }
  }
  
  override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    
    tableView.deselectRow(at: indexPath, animated: true)
    
    switch indexPath.row {
    case 1:
      performSegue(withIdentifier: Segue.importWalletSegue.rawValue, sender: nil)
    case 3:
      showSafariView()
    default:
      print(indexPath)
    }
  }

}
