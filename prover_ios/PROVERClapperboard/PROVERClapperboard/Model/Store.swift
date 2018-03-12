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
      case .success(let data):
        print(data)
      case .failure(let error):
        print(error)
      }
    }
  }
  
  /*
  func check() {
    
    let txhash = "0xcb300e9aeff7ecba16017b661ac1a0d14a33137e92d3707b45c5f6d68e419892"
    provider.request(.check(txhash: txhash)) { result in
      switch result {
      case let .success(responce):
        do {
          guard let json = try JSONSerialization.jsonObject(with: responce.data, options: .allowFragments)
            as? [String: Any] else {
              print("Can't create dictionary from response data in check request")
              return
          }
          print(json)
        } catch {
          print(error.localizedDescription)
        }
      case let .failure(error):
        print(error)
      }
    }
  }
 */
}
