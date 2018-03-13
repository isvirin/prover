import Foundation

protocol CycleCheckOperationDataProvider {
  var outputToCycleCheck: APIStringResult? { get }
}

class CycleCheckOperation: AsyncOperation {
  
  let apiService: APIService
  
  var input: APIStringResult?
  var result: APIStringResult?
  
  init(apiService: APIService) {
    self.apiService = apiService
  }
  
  override func main() {
    if let dependency = dependencies
      .filter({ $0 is CycleCheckOperationDataProvider })
      .first as? CycleCheckOperationDataProvider,
      input == nil {
      input = dependency.outputToCycleCheck
    }
    
    guard let input = input else { return }
    
    switch input {
    case .success(let txHash):
            
      let queue = OperationQueue()
      var isContinue = true
      
      while isContinue && !isCancelled {
        sleep(5)
        let checkOperation = CheckOperation(apiService: apiService, txHash: txHash)
        checkOperation.completionBlock = { [unowned self, unowned operation = checkOperation] in
          if let result = operation.result {
            if let blockHash = result.value {
              self.result = .success(blockHash)
              isContinue = false
            }
          }
        }
        queue.addOperations([checkOperation], waitUntilFinished: true)
      }
      
      state = .isFinished
      
    case .failure(let error):
      result = .failure(error)
      self.state = .isFinished
    }
  }
}
