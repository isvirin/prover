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
      
      let isFailure = true
      
      while isFailure {
        sleep(5)
        
        let checkOperation = CheckOperation(apiService: apiService, txHash: txHash)
        checkOperation.completionBlock = {
          print(checkOperation.result)
        }
        checkOperation.start()
        
      }
      
    case .failure(let error):
      result = .failure(error)
      self.state = .finished
    }
    
  }
}
