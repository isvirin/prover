import Foundation

class GetInfoOperation: AsyncOperation {
  
  let apiService: APIService
  let hex: String
  var result: APIInfoResult? {
    didSet {
      print(result)
    }
  }
  
  init(apiService: APIService, hex: String) {
    self.apiService = apiService
    self.hex = hex
  }
  
  override func main() {
    apiService.getInfo(hex: hex) { (result) in
      self.result = result
      self.state = .finished
    }
  }
}
