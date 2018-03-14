import Foundation
import Moya

class DependencyStore {

  // MARK: - Dependencies
  let ethereumService = EthereumService.shared
  let apiService = APIService()
}
