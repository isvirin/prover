import Foundation
import Moya
import Result

enum APIError: Error {
  case convertationError
  case networkError
}

typealias APIResult = Result<Info, APIError>

class APIService {
  
  let provider = MoyaProvider<ProverAPI>()
  
  func getInfo(hex: String, handler: @escaping (APIResult) -> Void) {
    
    provider.request(.hello(hex: hex)) { (result) in
      
      switch result {
      case .success(let responce):
        guard let infoResult = try? JSONDecoder().decode(InfoReslut.self, from: responce.data) else {
          handler(.failure(.convertationError))
          return
        }
        guard let info = Info(from: infoResult) else {
          handler(.failure(.convertationError))
          return
        }
        handler(.success(info))
      case .failure:
        handler(.failure(.networkError))
      }
      
    }
  }
}
