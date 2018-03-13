import Foundation
import UIKit
import Result

typealias DataResult = Result<[UInt8], APIError>

class QRCodeDataOperation: AsyncOperation {
  
  let apiService: APIService
  let ethereumService: EthereumService
  let text: String
  
  var result: DataResult?
  var txHash: Hexadecimal?
  var blockHash: Hexadecimal?
  
  init(apiService: APIService, ethereumService: EthereumService, text: String) {
    self.apiService = apiService
    self.ethereumService = ethereumService
    self.text = text
  }
  
  override func main() {
    
    let queue = OperationQueue()
    
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
      
      if let value = operation.result?.value {
        self.blockHash = Hexadecimal(value)
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
    
    state = .finished
  }
}
