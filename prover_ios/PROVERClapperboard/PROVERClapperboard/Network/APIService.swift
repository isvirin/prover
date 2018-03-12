import Foundation
import Moya
import Result

enum APIError: Error {
  case convertationError
  case networkError
  case submitError(SubmitRequestError)
}

typealias APIInfoResult = Result<Info, APIError>
typealias APISubmitResult = Result<String, APIError>

class APIService {
  
  let provider = MoyaProvider<ProverAPI>()
  
  func getInfo(hex: String, handler: @escaping (APIInfoResult) -> Void) {
    
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
  
  func submit(hex: String, handler: @escaping (APISubmitResult) -> Void) {
    
    provider.request(.submit(hex: hex)) { result in
      
      switch result {
      case .success(let responce):
        guard let submitResult = try? JSONDecoder().decode(SubmitResult.self, from: responce.data) else {
          handler(.failure(.convertationError))
          return
        }
        switch submitResult {
        case .result(let text):
          handler(.success(text))
        case .error(let error):
          handler(.failure(.submitError(error)))
        }
      case .failure:
        handler(.failure(.networkError))
      }
    }
  }
}
