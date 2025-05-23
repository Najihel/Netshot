import { ListItem, Skeleton, Stack, Tag, Text, UnorderedList } from "@chakra-ui/react";
import { useQuery } from "@tanstack/react-query";
import { createColumnHelper } from "@tanstack/react-table";
import { useCallback, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";

import api from "@/api";
import { NetshotError } from "@/api/httpClient";
import { DataTable, EmptyResult } from "@/components";
import Search from "@/components/Search";
import { usePagination, useToast } from "@/hooks";
import { DeviceInterface } from "@/types";
import { merge, search } from "@/utils";

import { QUERIES } from "../constants";

const columnHelper = createColumnHelper<DeviceInterface>();

/**
 * @todo: Add pagination (paginator)
 */
export default function DeviceInterfaceScreen() {
  const params = useParams<{ id: string }>();
  const { t } = useTranslation();
  const toast = useToast();
  const pagination = usePagination();

  const { data = [], isPending } = useQuery({
    queryKey: [
      QUERIES.DEVICE_INTERFACES,
      params.id,
      pagination.query,
      pagination.offset,
      pagination.limit,
    ],
    queryFn: async () =>
        api.device.getAllInterfacesById(+params.id, pagination),
    select: useCallback((res: DeviceInterface[]): DeviceInterface[] => {
      // Search on IP addresses
      const passA = res.filter((item) => {
        return item.ip4Addresses.some((address) => {
          return (
            address.ip.toLocaleLowerCase().startsWith(pagination.query) ||
            address.ip.toLocaleLowerCase().includes(pagination.query)
          );
        });
      });

      // Search on other props
      const passB = search(
        res,
        "description",
        "interfaceName",
        "macAddress",
        "vrfInstance"
      ).with(pagination.query);

      // Merge two results in one
      return merge(passA, passB, "id");
    }, [pagination.query]),
  });

  const columns = useMemo(() => [
    data.find(i => !!i.virtualDevice) &&
      columnHelper.accessor("virtualDevice", {
        cell: (info) => info.getValue(),
        header: t("Virtual device"),
        enableSorting: true,
        size: 10000,
      }),
    columnHelper.accessor("interfaceName", {
      cell: (info) => info.getValue(),
      header: t("Name"),
      enableSorting: true,
      size: 10000,
    }),
    columnHelper.accessor("enabled", {
      cell: (info) => info.getValue() === false ?
        <Tag colorScheme="red">{t("Disabled")}</Tag> : null,
      header: "",
      size: 100,
    }),
    columnHelper.accessor("level3", {
      cell: (info) => info.getValue() === false ?
        <Tag colorScheme="grey">{t("L2")}</Tag> : null,
      header: "",
      size: 100,
    }),
    columnHelper.accessor("description", {
      cell: (info) => info.getValue(),
      header: t("Description"),
      enableSorting: true,
      size: 20000,
    }),
    columnHelper.accessor("macAddress", {
      cell: (info) => {
        if (info.getValue() && info.getValue() !== "0000.0000.0000") {
          return info.getValue();
        }
        return "";
      },
      header: t("MAC Address"),
      enableSorting: true,
      size: 100,
    }),
    data.find(i => !!i.vrfInstance) &&
      columnHelper.accessor("vrfInstance", {
        cell: (info) => info.getValue(),
        header: t("VRF"),
        enableSorting: true,
        size: 10000,
      }),
    columnHelper.accessor("ip4Addresses", {
      cell: (info) => {
        const value = info.getValue();
        if (value?.length > 0) {
          return (
            <UnorderedList listStyleType="''" lineHeight="1.5em">
              {value.map((i) => (
                <ListItem key={i.ip}>
                  <Text as="span">{i.ip}</Text><Text as="span" color="gray.300">/{i.prefixLength}</Text>
                </ListItem>
              ))}
            </UnorderedList>
          );
        }
        return "";
      },
      header: t("IP Address"),
      enableSorting: true,
      size: 100,
    }),
  ].filter(c => !!c), [t, data]);

  return (
    <Stack spacing="6" overflow="auto">
      <Stack direction="row" spacing="3">
        <Search
          placeholder={t(
            "Search by virtual device, name, description, mac address, ip..."
          )}
          onQuery={pagination.onQuery}
          w="30%"
        />
      </Stack>

      {isPending ? (
        <Stack spacing="3">
          <Skeleton h="60px"></Skeleton>
          <Skeleton h="60px"></Skeleton>
          <Skeleton h="60px"></Skeleton>
          <Skeleton h="60px"></Skeleton>
        </Stack>
      ) : (
        <>
          {data?.length > 0 ? (
            <DataTable columns={columns} data={data} loading={isPending} />
          ) : (
            <EmptyResult
              title={t("There is no interface")}
              description={t(
                "This device does not have any interface, please check his configuration"
              )}
            />
          )}
        </>
      )}
    </Stack>
  );
}
