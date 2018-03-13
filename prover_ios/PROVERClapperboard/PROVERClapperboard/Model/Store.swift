import Foundation
import Moya
import Repeat

class Store {

  var txHash: String? {
    didSet {
      if let value = txHash {
        print("Current txHash: \(value)")
      }
    }
  }
  
  var repeater: Repeater?
  
  var blockHash: String? {
    didSet {
      if let value = blockHash {
        print("Current blockHash: \(value)")
        repeater = nil
      }
    }
  }

  // MARK: - Dependencies
  let ethereumService = EthereumService.shared
  let apiService = APIService()

  // MARK: - Istanse properties
  var info: Info?

  // MARK: - Initializaton
  init() {
    
    let queue = OperationQueue()
    
    let qrCodeOperation = QRCodeOperation(apiService: apiService,
                                          ethereumService: ethereumService,
                                          text: "test")
    qrCodeOperation.completionBlock = {
      print(qrCodeOperation.result)
    }
    
    queue.addOperations([qrCodeOperation], waitUntilFinished: false)
  }
}
