export interface Metadata {
  name: string;
  labels?: {
    [key: string]: string;
  } | null;
  annotations?: {
    [key: string]: string;
  } | null;
  version?: number | null;
  creationTimestamp?: string | null;
  deletionTimestamp?: string | null;
}

export interface PcdnSpec {
  accesskey: string;
  secret: string;
  ObjectPath: string;
  ObjectType: string;
  done?: boolean;
}

/**
 * 与自定义模型 Todo 对应
 */
export interface Pcdn {
  spec: PcdnSpec;
  apiVersion: "pcdn.plugin.halo.run/v1alpha1"; // apiVersion=自定义模型的 group/version
  kind: "Pcdn"; // Todo 自定义模型中 @GVK 注解中的 kind
  metadata: Metadata;
}

/**
 * Todo 自定义模型生成 list API 所对应的类型
 */
export interface PcdnList {
  page: number;
  size: number;
  total: number;
  items: Array<Pcdn>;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
  totalPages: number;
}

