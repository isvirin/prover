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
    getQRCodeData(from: "test")
  }
  
  // MARK: - Network request
  func getQRCodeData(from text: String) {
    
    let getInfoOperation = GetInfoOperation(apiService: apiService, hex: ethereumService.hexAddress)
    
    let submitOperation = SubmitOperation(apiService: apiService,
                                          ethereumService: ethereumService,
                                          text: text)
    submitOperation.addDependency(getInfoOperation)
    
    let queue = OperationQueue()
    queue.addOperations([getInfoOperation, submitOperation], waitUntilFinished: false)
  }
  
  private func updateInfo() {
    
    apiService.getInfo(hex: ethereumService.hexAddress) { (result) in
      switch result {
      case .success(let data):
        self.info = data
      case .failure(let error):
        print(error)
      }
    }
  }
  
  private func submit(message: String) {
    
    let transactionHex = ethereumService.getTransactionHex(from: message,
                                                           nonce: info!.nonce,
                                                           contractAddress: info!.contractAddress,
                                                           gasPrice: info!.gasPrice)
    
    apiService.submit(hex: transactionHex.withPrefix) { (result) in
      switch result {
      case .success(let text):
        print("Start check request")
        self.check(txhash: text)
      case .failure(let error):
        print(error)
      }
    }
  }
  
  private func check(txhash: String) {
    
    apiService.check(txhash: txhash) { (result) in
      switch result {
      case .success(let data):
        print(data)
      case .failure(let error):
        print(error)
      }
    }
  }
}
