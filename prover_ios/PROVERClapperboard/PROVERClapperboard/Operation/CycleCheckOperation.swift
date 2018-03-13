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
    print("Start cycle check operation")
    if let dependency = dependencies
      .filter({ $0 is CycleCheckOperationDataProvider })
      .first as? CycleCheckOperationDataProvider,
      input == nil {
      input = dependency.outputToCycleCheck
    }
    
    guard let input = input else { return }
    
    switch input {
    case .success(let txHash):
      
      print("Start check cycle for txHash: \(txHash)")
      
      let queue = OperationQueue()
      var isContinue = true
      
      while isContinue {
        sleep(10)
        print("Start new check cycle")
        let checkOperation = CheckOperation(apiService: apiService, txHash: txHash)
        checkOperation.completionBlock = { [unowned self, unowned operation = checkOperation] in
          print("Check operation completion block")
          if let result = operation.result {
            if let blockHash = result.value {
              print(blockHash)
              self.result = .success(blockHash)
              isContinue = false
            }
          }
        }
        queue.addOperations([checkOperation], waitUntilFinished: true)
        print("queue finish")
      }
      
      state = .finished
      
    case .failure(let error):
      result = .failure(error)
      self.state = .finished
    }
  }
  
  deinit {
    print("Deinit cycle check operation")
  }
}
