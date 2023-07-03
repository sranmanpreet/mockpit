export interface Mock {
  name: string,
  description: string,
  responseBody: ResponseBody,
  responseStatus: ResponseStatus,
  route: Route,
  responseHeaders: Array<ResponseHeader>
}

interface ResponseBody {
  content: any;
}

interface ResponseStatus {
  code: number
}

interface Route {
  path: string,
  method: "GET" | "PUT" | "POST" | "PATCH" | "DELETE" | "OPTIONS"
}

interface ResponseHeader {
  name: string,
  value: string
}

export interface MockResponse {
  message: string,
  data: any
}