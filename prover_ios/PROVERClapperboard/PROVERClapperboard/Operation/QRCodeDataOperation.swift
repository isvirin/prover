import Foundation
import UIKit
import Result

typealias DataResult = Result<[UInt8], APIError>

class QRCodeDataOperation: AsyncOperation {
  
  let apiService: APIService
  let ethereumService: EthereumService
  let text: String
  
  let queue = OperationQueue()
  
  var result: DataResult?
  var txHash: Hexadecimal?
  var blockHash: Hexadecimal?
  
  init(apiService: APIService, ethereumService: EthereumService, text: String) {
    self.apiService = apiService
    self.ethereumService = ethereumService
    self.text = text
  }
  
  override func main() {
    
    let getInfoOperation = GetInfoOperation(apiService: apiService, hex: ethereumService.hexAddress)
    
    let submitOperation = SubmitOperation(apiService: apiService,
                                          ethereumService: ethereumService,
                                          text: text)
    submitOperation.addDependency(getInfoOperation)
    submitOperation.completionBlock = { [unowned self, unowned operation = submitOperation] in
      if let value = operation.result?.value {
        self.txHash = Hexadecimal(value)
      }
    }
    
    let cycleCheckOperation = CycleCheckOperation(apiService: apiService)
    cycleCheckOperation.addDependency(submitOperation)
    
    let outputOperation = BlockOperation { [unowned self, unowned operation = cycleCheckOperation] in
      
      guard let result = operation.result else {
        print("cycle check operation return nil")
        return
      }
      
      switch result {
      case .success(let blockHash):
        self.blockHash = Hexadecimal(blockHash)
      case .failure(let error):
        self.result = .failure(error)
        return
      }
      
      guard let txHash = self.txHash, let blockHash = self.blockHash else {
        self.result = .failure(APIError.convertToHexError)
        return
      }
      
      self.result = .success(txHash.toBytes + blockHash.toBytes.prefix(14))
    }
    outputOperation.addDependency(cycleCheckOperation)
    
    queue.addOperations([getInfoOperation, submitOperation, cycleCheckOperation, outputOperation],
                        waitUntilFinished: true)
    
    state = .isFinished
  }
  
  override func cancel() {
    queue.cancelAllOperations()
    super.cancel()
  }
}
