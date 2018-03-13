import UIKit

class QRCodeViewController: UIViewController {
  
  // MARK: - IBOutlet
  @IBOutlet weak var qrText: UILabel! {
    didSet {
      qrText.text = text
    }
  }
  @IBOutlet weak var qrImage: UIImageView!
  
  // MARK: - IBAction
  @IBAction func closeButtonAction(_ sender: UIButton) {
    dismiss(animated: true, completion: nil)
  }
  
  // MARK: - Dependency
  var store: Store!
  
  // MARK: - Instance properties
  var text: String?
  
  // MARK: - Lifecycle
  override func viewDidLoad() {
    super.viewDidLoad()
    
    generateQR()
  }
  
  // MARK: - Private methods
  func generateQR() {
    
    guard let text = text else { return }
    
    let queue = OperationQueue()
    let qrDataOperation = QRCodeDataOperation(apiService: store.apiService,
                                              ethereumService: store.ethereumService,
                                              text: text)
    qrDataOperation.completionBlock = { [unowned self, unowned operation = qrDataOperation] in
      guard let result = operation.result else {
        print("qrDataOperation return nil")
        return
      }
      switch result {
      case .success(let data):
        print(data)
        DispatchQueue.main.async {
          self.qrImage.image = QRCoder().encode(data)
        }
      case .failure(let error):
        print(error)
      }
    }
    queue.addOperation(qrDataOperation)
  }
}
