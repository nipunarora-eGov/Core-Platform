import { useQuery, useQueryClient } from "react-query";
import { Mdmsv2Service } from "../services/elements/Mdmsv2Service";

const useCustomMdmsv2 = ({ url, params, body, config = {}, plainAccessRequest,changeQueryName="Random",...props }) => {
  const client = useQueryClient();

  const { isLoading, data, isFetching } = useQuery(
    [url,changeQueryName].filter((e) => e),
    () => Mdmsv2Service.getResponse({ url, params, body, plainAccessRequest }),
    {
      cacheTime:0,
      ...config,
    }
  );

  return {
    isLoading,
    isFetching,
    data,
    revalidate: () => {
      data && client.invalidateQueries({ queryKey: [url].filter((e) => e) });
    },
  };
};

export default useCustomMdmsv2;