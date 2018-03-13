import Foundation

class QRCodeOperation: AsyncOperation {
  
  let apiService: APIService
  let ethereumService: EthereumService
  let text: String
  
  var result: APIStringResult?
  
  init(apiService: APIService, ethereumService: EthereumService, text: String) {
    self.apiService = apiService
    self.ethereumService = ethereumService
    self.text = text
  }
  
  override func main() {
    print("Start QRCode operation")
    let getInfoOperation = GetInfoOperation(apiService: apiService, hex: ethereumService.hexAddress)
    
    let submitOperation = SubmitOperation(apiService: apiService,
                                          ethereumService: ethereumService,
                                          text: text)
    submitOperation.addDependency(getInfoOperation)
    
    let cycleCheckOperation = CycleCheckOperation(apiService: apiService)
    cycleCheckOperation.addDependency(submitOperation)
    cycleCheckOperation.completionBlock = {
      self.result = cycleCheckOperation.result
    }
    
    let queue = OperationQueue()
    queue.addOperations([getInfoOperation, submitOperation, cycleCheckOperation], waitUntilFinished: true)
    
    state = .finished
  }
}
