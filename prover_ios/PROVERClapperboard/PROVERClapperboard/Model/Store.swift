import Foundation
import Moya

class Store {

  // MARK: - Dependencies
  let ethereumService = EthereumService.shared
  let provider = MoyaProvider<ProverAPI>()

  // MARK: - Istanse properties
  var info: InfoReslut? {
    didSet {
      print(info)
    }
  }

  // MARK: - Initializaton
  init() {
    updateBalance()
  }
  
  // MARK: - Network request
  func updateBalance() {
    
    provider.request(.hello(hex: ethereumService.hexAddress)) { result in
      
      switch result {
      case let .success(responce):
        self.info = try? JSONDecoder().decode(InfoReslut.self, from: responce.data)
      case let .failure(error):
        print(error)
      }
    }
  }
  
  /*
  func submit(message: String) {
    
    let transactionHex = ethereumService.getTransactionHex(from: message,
                                                           nonce: Hexadecimal(info!.nonce!)!,
                                                           contractAddress: Hexadecimal(info!.contractAddress)!,
                                                           gasPrice: Hexadecimal(info!.gasPrice)!)
    print("transactionHex \(transactionHex)")
    
    provider.request(.submit(hex: transactionHex.withPrefix)) { result in
      
      switch result {
      case let .success(responce):
        do {
          guard let json = try JSONSerialization.jsonObject(with: responce.data, options: .allowFragments)
            as? [String: Any] else {
            print("Can't create dictionary from response data in submit request")
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
