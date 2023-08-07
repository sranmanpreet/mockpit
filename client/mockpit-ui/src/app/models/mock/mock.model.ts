export interface Mock {
  id: number,
  name: string,
  description: string,
  active?: boolean,
  responseBody: ResponseBody,
  responseStatus: ResponseStatus,
  route: Route,
  responseHeaders: Array<ResponseHeader>
}

interface ResponseBody {
  content: any;
  contentType: any;
  type: any;
}

interface ResponseStatus {
  code: number
}

interface Route {
  path: string,
  method: "GET" | "PUT" | "POST" | "PATCH" | "DELETE" | "OPTIONS"
}

export interface ResponseHeader {
  name: string,
  value: string
}

export interface MockResponse {
  message: string,
  data: any
}