import Foundation
import Moya

class Store {

  // MARK: - Dependencies
  let ethereumService = EthereumService.shared
  let apiService = APIService()

  // MARK: - Istanse properties
  var info: Info? {
    didSet {
      if info != nil {
        print("Start submit request")
        submit(message: "test")
      }
    }
  }

  // MARK: - Initializaton
  init() {
    updateInfo()
  }
  
  // MARK: - Network request
  func updateInfo() {
    
    apiService.getInfo(hex: ethereumService.hexAddress) { (result) in
      switch result {
      case .success(let data):
        self.info = data
      case .failure(let error):
        print(error)
      }
    }
  }
  
  func submit(message: String) {
    
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
  
  func check(txhash: String) {
    
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
