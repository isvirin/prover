import Foundation
import Moya
import Repeat

class Store {
  
  var timer: Repeater?
  var txHash: String? {
    didSet {
      if let value = txHash {
        print("Current txHash: \(value)")
      }
    }
  }
  var blockHash: String? {
    didSet {
      if let value = blockHash {
        print("Current blockHash: \(blockHash)")
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
    getQRCodedata(from: "test")
  }
  
  // MARK: - Network request
  func getQRCodedata(from text: String) {
    
    print("Start getInfo")
    apiService.getInfo(hex: ethereumService.hexAddress) { (result) in
      switch result {
      case .success(let info):
        
        print("nonce: \(info.nonce.withPrefix)")
        
        let transactionHex = self.ethereumService.getTransactionHex(from: text,
                                                               nonce: info.nonce,
                                                               contractAddress: info.contractAddress,
                                                               gasPrice: info.gasPrice)
        
        print("Start submit request")
        self.apiService.submit(hex: transactionHex.withPrefix) { (result) in
          switch result {
          case .success(let txhash):
            
            self.timer = Repeater.every(.seconds(2), count: 10) { _ in
              print("Start timer")
              print("Start check request")
              self.apiService.check(txhash: txhash) { (result) in
                switch result {
                case .success(let blockHash):
                  print("txhash: \(txhash)")
                  print("blockHash: \(blockHash)")
                case .failure(let error):
                  print(error)
                }
              }
            }
            self.timer?.start()
            
          case .failure(let error):
            print(error)
          }
        }
      case .failure(let error):
        print(error)
      }
    }
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
