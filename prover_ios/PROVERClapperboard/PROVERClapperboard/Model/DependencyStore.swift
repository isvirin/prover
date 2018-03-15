import Foundation
import Moya

class DependencyStore {

  // MARK: - Dependencies
  let ethereumService = EthereumService.shared
  let apiService = APIService()
  
  init() {
    print(FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!)
    print(ethereumService.hexAddress)
  }
}
