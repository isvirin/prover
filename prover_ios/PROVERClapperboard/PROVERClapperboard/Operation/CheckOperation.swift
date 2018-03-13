import Foundation

class CheckOperation: AsyncOperation {
  
  let apiService: APIService
  let txHash: String
  
  var inputSubmitResult: APIStringResult?
  var result: APIStringResult?
  
  init(apiService: APIService, txHash: String) {
    self.apiService = apiService
    self.txHash = txHash
  }
  
  override func main() {
    print("Start check operation on time \(Date())")
    apiService.check(txhash: txHash) { (result) in
      self.result = result
      self.state = .isFinished
    }
  }
}
