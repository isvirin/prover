import Foundation
import Moya

class DependencyStore {

  // MARK: - Dependencies
  let ethereumService = EthereumService.shared
  let apiService = APIService()
  
  init() {
    print("path to documents: \(FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!)")
    print("wallet address: \(ethereumService.hexAddress)")
  }
}
