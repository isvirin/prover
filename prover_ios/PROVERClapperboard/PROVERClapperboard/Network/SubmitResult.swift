import Foundation

enum SubmitResult: Decodable {
  case result(String)
  case error(SubmitRequestError)
  
  enum CodingKeys: String, CodingKey {
    case result
    case error
  }
  
  init(from decoder: Decoder) throws {
    
    let values = try decoder.container(keyedBy: CodingKeys.self)
    
    if let result = try? values.decode(String.self, forKey: .result) {
      self = .result(result)
    } else {
     self = .error(try values.decode(SubmitRequestError.self, forKey: .error))
    }
  }
}
