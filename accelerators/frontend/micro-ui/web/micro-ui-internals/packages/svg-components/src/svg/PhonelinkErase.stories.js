import React from "react";
import { PhonelinkErase } from "./PhonelinkErase";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PhonelinkErase",
  component: PhonelinkErase,
};

export const Default = () => <PhonelinkErase />;
export const Fill = () => <PhonelinkErase fill="blue" />;
export const Size = () => <PhonelinkErase height="50" width="50" />;
export const CustomStyle = () => <PhonelinkErase style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PhonelinkErase className="custom-class" />;
