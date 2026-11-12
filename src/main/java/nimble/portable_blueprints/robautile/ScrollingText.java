package nimble.portable_blueprints.robautile;

public class ScrollingText {
   private String TestoOriginale;
   private String Testo;
   private int SliderTestoX;
   private int MaxNameTextChar;
   public String TestoVisualizzabile = "";

   public ScrollingText(String Testo, int MaxNameTextChar) {
      this.TestoOriginale = Testo;

      for (int x = 0; x < MaxNameTextChar / 3; x++) {
         Testo = Testo + " ";
      }

      this.Testo = Testo;
      this.SliderTestoX = 0;
      this.MaxNameTextChar = MaxNameTextChar;
      this.update();
   }

   public void update() {
      if (this.Testo.length() > this.MaxNameTextChar + this.MaxNameTextChar / 3) {
         String testoRisultato = "";
         if (++this.SliderTestoX > this.Testo.length() - 1) {
            this.SliderTestoX = 0;
         }

         for (int x = 0; x < this.MaxNameTextChar; x++) {
            int newVal = this.SliderTestoX + x;
            if (newVal >= this.Testo.length()) {
               newVal = this.SliderTestoX + x - this.Testo.length();
            }

            testoRisultato = testoRisultato + this.Testo.toCharArray()[newVal];
         }

         this.TestoVisualizzabile = testoRisultato;
      } else {
         this.TestoVisualizzabile = this.TestoOriginale;
      }
   }

   public void changeText(String testo) {
      this.TestoOriginale = testo;

      for (int x = 0; x < this.MaxNameTextChar / 3; x++) {
         testo = testo + " ";
      }

      this.Testo = testo;
      if (this.SliderTestoX > this.Testo.length() - 1) {
         this.SliderTestoX = 0;
      }
   }
}
