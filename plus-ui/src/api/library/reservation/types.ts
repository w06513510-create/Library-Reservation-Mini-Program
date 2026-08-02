export interface ReservationVO {
  id: string | number;
  readerId: string | number;
  seatId: string | number;
  venueId?: string | number;
  floorId?: string | number;
  areaId?: string | number;
  reserveDate: string;
  startTime: string;
  endTime: string;
  source: number;
  status: number;
  checkInTime?: string;
  awayCount?: number;
  actualEndTime?: string;
  cancelTime?: string;
  remark?: string;
}

export interface ReservationForm extends BaseEntity {
  id?: string | number;
  readerId?: string | number;
  seatId?: string | number;
  reserveDate?: string;
  startTime?: string;
  endTime?: string;
  source?: number;
}

export interface ReservationQuery extends PageQuery {
  readerId?: string | number;
  seatId?: string | number;
  status?: number;
  reserveDate?: string;
}
