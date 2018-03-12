import Foundation

protocol CheckOperationDataProvider {
  var txHashResult: APIStringResult? { get }
}

class CheckOperation: AsyncOperation {
  
  let apiService: APIService
  
  var inputSubmitResult: APIStringResult?
  var result: APIStringResult?
  
  init(apiService: APIService) {
    self.apiService = apiService
  }
  
  override func main() {
    
    if let dependency = dependencies
      .filter({ $0 is CheckOperationDataProvider })
      .first as? CheckOperationDataProvider,
      inputSubmitResult == nil {
      inputSubmitResult = dependency.txHashResult
    }
    
    guard let inputSubmitResult = inputSubmitResult else { return }
    
    switch inputSubmitResult {
    case .success(let txHash):
      
      apiService.check(txhash: txHash) { (result) in
        self.result = result
        self.state = .finished
      }
    case .failure(let error):
      result = .failure(error)
      self.state = .finished
    }
  }
}
