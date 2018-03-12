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
    
    print("Start getInfo")
    apiService.getInfo(hex: ethereumService.hexAddress) { [unowned self] (result) in
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
            
            self.txHash = txhash
            
            if self.repeater == nil {
              
              self.repeater = Repeater(interval: .seconds(10), mode: .infinite) { _ in
                print("Start check request")
                guard let txHash = self.txHash else {
                  print("txhash equal nil")
                  return
                }
                
                self.apiService.check(txhash: txHash) { (result) in
                  switch result {
                  case .success(let blockHash):
                    print("Success")
                    self.blockHash = blockHash
                  case .failure(let error):
                    print(error)
                  }
                }
              }
              
              self.repeater?.start()
            }
            
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
