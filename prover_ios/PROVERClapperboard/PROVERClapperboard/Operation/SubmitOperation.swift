import Foundation

protocol SubmitOperationDataProvider {
  var outputToSubmit: APIInfoResult? { get }
}

class SubmitOperation: AsyncOperation {
  
  let apiService: APIService
  let ethereumService: EthereumService
  let text: String
  
  var input: APIInfoResult?
  var result: APIStringResult?
  
  init(apiService: APIService, ethereumService: EthereumService, text: String) {
    self.apiService = apiService
    self.ethereumService = ethereumService
    self.text = text
  }
  
  override func main() {
    print("Start submit operation")
    if let dependency = dependencies
      .filter({ $0 is SubmitOperationDataProvider })
      .first as? SubmitOperationDataProvider,
      input == nil {
      input = dependency.outputToSubmit
    }
    
    guard let input = input else { return }
    
    switch input {
    case .success(let info):
      
      let transactionHex = ethereumService.getTransactionHex(from: text,
                                                             nonce: info.nonce,
                                                             contractAddress: info.contractAddress,
                                                             gasPrice: info.gasPrice)
      
      apiService.submit(hex: transactionHex.withPrefix) { (result) in
        self.result = result
        self.state = .finished
      }
    case .failure(let error):
      result = .failure(error)
      self.state = .finished
    }
  }
}

extension SubmitOperation: CycleCheckOperationDataProvider {
  var outputToCycleCheck: APIStringResult? {
    return result
  }
}
