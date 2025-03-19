package dat.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dat.entities.Room;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomDTO {
    private Integer roomId;
    private Integer roomNumber;
    private Integer roomPrice;
    private Room.RoomType roomType;

    public RoomDTO(Room room) {
        this.roomId = room.getRoomId();
        this.roomNumber = room.getRoomNumber();
        this.roomPrice = room.getRoomPrice().intValue();
        this.roomType = room.getRoomType();
    }

    public static List<RoomDTO> toRoomDTOList(List<Room> rooms) {
        return List.of(rooms.stream().map(RoomDTO::new).toArray(RoomDTO[]::new));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        RoomDTO roomDTO = (RoomDTO) o;
        return getRoomId().equals(roomDTO.getRoomId()) && getRoomNumber().equals(roomDTO.getRoomNumber()) && getRoomPrice().equals(roomDTO.getRoomPrice()) && getRoomType() == roomDTO.getRoomType();
    }

    @Override
    public int hashCode() {
        int result = getRoomId().hashCode();
        result = 31 * result + getRoomNumber().hashCode();
        result = 31 * result + getRoomPrice().hashCode();
        result = 31 * result + getRoomType().hashCode();
        return result;
    }
}
