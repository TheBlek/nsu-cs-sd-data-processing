import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "people")
class People {
    @XmlElement(name = "person")
    List<FinalPerson> people = new ArrayList<>();
}
