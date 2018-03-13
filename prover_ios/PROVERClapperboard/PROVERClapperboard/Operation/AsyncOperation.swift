import Foundation

class AsyncOperation: Operation {
  // DONE: State enum with keyPath property
  enum State: String {
    case ready = "Ready"
    case executing = "Executing"
    case finished = "Finished"
    
    fileprivate var keyPath: String {
      return "is" + rawValue
    }
  }
  
  // DONE: state property
  var state = State.ready {
    willSet {
      willChangeValue(forKey: newValue.keyPath)
      willChangeValue(forKey: state.keyPath)
    }
    didSet {
      didChangeValue(forKey: oldValue.keyPath)
      didChangeValue(forKey: state.keyPath)
    }
  }
}

extension AsyncOperation {
  // DONE: Operation Overrides
  override var isReady: Bool {
    return super.isReady && state == .ready
  }
  
  override var isExecuting: Bool {
    return state == .executing
  }
  
  override var isFinished: Bool {
    return state == .finished
  }
  
  override var isAsynchronous: Bool {
    return true
  }
  
  override func start() {
    if isCancelled {
      state = .finished
      return
    }
    main()
    state = .executing
  }
  
  override func cancel() {
    state = .finished
  }
  
}
