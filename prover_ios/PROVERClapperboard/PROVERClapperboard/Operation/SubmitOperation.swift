import Foundation

protocol SubmitOperationInfoProvider {
  var outputInfoResult: APIInfoResult? { get }
}

class SubmitOperation: AsyncOperation {
  
  let apiService: APIService
  let ethereumService: EthereumService
  let text: String
  
  var inputInfoResult: APIInfoResult?
  var result: APIStringResult?
  
  init(apiService: APIService, ethereumService: EthereumService, text: String) {
    self.apiService = apiService
    self.ethereumService = ethereumService
    self.text = text
  }
  
  override func main() {
    
    if let dependency = dependencies
      .filter({ $0 is SubmitOperationInfoProvider })
      .first as? SubmitOperationInfoProvider,
      inputInfoResult == nil {
      inputInfoResult = dependency.outputInfoResult
    }
    
    guard let inputInfoResult = inputInfoResult else { return }
    
    switch inputInfoResult {
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

extension SubmitOperation: CheckOperationDataProvider {
  var txHashResult: APIStringResult? {
    return result
  }
}
