export enum PortType {
  ANALOG_INPUT = 0,
  DIGITAL_INPUT = 1,
  OUTPUT = 2
}

export const PortTypeLabels: Record<PortType, string> = {
  [PortType.ANALOG_INPUT]: 'Аналоговый вход',
  [PortType.DIGITAL_INPUT]: 'Цифровой вход',
  [PortType.OUTPUT]: 'Выход'
};

export interface Driver {
  id: number;
  name: string;
  type: PortType;
}

export interface Port {
  id: number;
  type: PortType;
}

export interface ModuleSummary {
  id: number;
  moduleUid: number;
  name: string | null;
  baseUrl: string;
  lastSeen: string;
}

export interface ModuleDetails {
  id: number;
  moduleUid: number;
  name: string | null;
  baseUrl: string;
  lastSeen: string;
  ports: Port[] | null;
  drivers: Driver[] | null;
  bindings: Binding[] | null;
}

export interface ModuleRegistrationRequest {
  baseUrl: string;
  name?: string | null;
}

export interface Binding {
  portId: number;
  driverId: number;
  driverName: string | null;
  createdAt: string;
}

export interface BindRequest {
  driverId: number;
}

export interface Measurement {
  portId: number;
  driverId: number;
  value: number;
  timestamp: string;
}

export interface WriteRequest {
  level: number;
}

export interface AutomationRule {
  id?: number;
  name: string;
  sourceModuleId: number;
  sourcePortId: number;
  condition: 'gt' | 'lt' | 'eq' | 'gte' | 'lte';
  threshold: number;
  targetModuleId: number;
  targetPortId: number;
  actionLevel: number;
  enabled: boolean;
}

export interface ErrorResponse {
  message: string;
}

