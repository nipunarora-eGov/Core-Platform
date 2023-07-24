import { Request,ServiceRequest } from "../atoms/Utils/Request";

export const Mdmsv2Service = {
  getResponse: ({ url, params, body, plainAccessRequest,useCache=true,userService=true,setTimeParam=true ,userDownload=false}) =>
    Request({
      url: url,
      data: body,
      useCache,
      userService,
      method: "POST",
      auth: true,
      params: params,
      plainAccessRequest: plainAccessRequest,
      userDownload:userDownload,
      setTimeParam
    })
};


